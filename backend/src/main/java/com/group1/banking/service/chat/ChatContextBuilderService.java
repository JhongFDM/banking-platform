package com.group1.banking.service.chat;

import com.group1.banking.dto.SavingsGoalResponse;
import com.group1.banking.dto.response.SpendingInsightResponse;
import com.group1.banking.entity.Account;
import com.group1.banking.entity.AccountStatus;
import com.group1.banking.enums.ChatTopic;
import com.group1.banking.exception.PermissionDeniedException;
import com.group1.banking.repository.AccountRepository;
import com.group1.banking.security.CustomUserPrincipal;
import com.group1.banking.service.SavingsGoalService;
import com.group1.banking.service.SpendingInsightService;
import com.group1.banking.service.chat.vectorstore.PgVectorKnowledgeStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Assembles everything the response generator is allowed to know, by
 * calling the app's *existing* ownership-checked services --
 * SpendingInsightService for this month's spending breakdown,
 * SavingsGoalService for the customer's goal, AccountRepository for status
 * -- rather than querying entities directly. That's a deliberate security
 * choice: the chatbot can't become a side door around authorization checks
 * that already exist elsewhere in the app, because it's reusing the same
 * services everything else uses.
 *
 * If the account is frozen/inactive, this stops there and flags
 * accountRestricted instead of including any balance or spend detail. If a
 * data source throws (no transactions yet, no goal set, a transient
 * failure), that section is marked "limited" rather than failing the whole
 * request.
 */
@Service
public class ChatContextBuilderService {

    private final AccountRepository accountRepository;
    private final SpendingInsightService spendingInsightService;
    private final SavingsGoalService savingsGoalService;
    private final PgVectorKnowledgeStore knowledgeStore;

    @Value("${pgvector.top-k:2}")
    private int topK;

    public ChatContextBuilderService(AccountRepository accountRepository,
                                      SpendingInsightService spendingInsightService,
                                      SavingsGoalService savingsGoalService,
                                      PgVectorKnowledgeStore knowledgeStore) {
        this.accountRepository = accountRepository;
        this.spendingInsightService = spendingInsightService;
        this.savingsGoalService = savingsGoalService;
        this.knowledgeStore = knowledgeStore;
    }

    public SafeChatContext build(CustomUserPrincipal caller, Long accountId, String rawQuery, ChatTopic topic) {
        boolean accountRestricted = false;
        boolean limitedTransactionData = true;
        boolean limitedGoalData = true;
        String topCategory = null;
        BigDecimal topCategoryAmount = null;
        BigDecimal totalSpend30d = null;
        boolean hasGoal = false;
        String goalName = null;
        BigDecimal goalProgressPercentage = null;
        BigDecimal goalTargetAmount = null;

        if (accountId != null) {
            Account account = accountRepository.findByAccountIdAndDeletedAtIsNull(accountId).orElse(null);

            if (account != null) {
                if (!account.getCustomer().getCustomerId().equals(caller.getCustomerId())) {
                    // Never trust a client-supplied accountId as proof of ownership.
                    throw new PermissionDeniedException("CHAT:ACCOUNT_OWNERSHIP");
                }

                if (account.getStatus() != AccountStatus.ACTIVE) {
                    accountRestricted = true;
                } else {
                    LocalDate today = LocalDate.now();
                    try {
                        SpendingInsightResponse insights = spendingInsightService.getInsights(
                                accountId, today.getYear(), today.getMonthValue(), caller);
                        totalSpend30d = insights.getTotalDebitSpend();
                        Optional<SpendingInsightResponse.CategoryBreakdownItem> top =
                                insights.getCategoryBreakdown() == null ? Optional.empty()
                                        : insights.getCategoryBreakdown().stream()
                                                .max(Comparator.comparing(SpendingInsightResponse.CategoryBreakdownItem::getTotalAmount));
                        if (top.isPresent()) {
                            topCategory = top.get().getCategory();
                            topCategoryAmount = top.get().getTotalAmount();
                        }
                        limitedTransactionData = false;
                    } catch (RuntimeException ex) {
                        // No transactions yet, or a transient failure -- degrade rather than fail the turn.
                        limitedTransactionData = true;
                    }

                    try {
                        SavingsGoalResponse goal = findGoalForAccount(caller.getCustomerId(), accountId);
                        if (goal != null) {
                            hasGoal = true;
                            goalName = goal.getGoalName();
                            goalProgressPercentage = goal.getProgressPercentage();
                            goalTargetAmount = goal.getTargetAmount();
                            limitedGoalData = false;
                        } else {
                            limitedGoalData = false; // no goal is a valid, known state -- not "limited"
                        }
                    } catch (RuntimeException ex) {
                        limitedGoalData = true;
                    }
                }
            }
        } else {
            // No account context requested -- fall back to any goal the customer has.
            try {
                List<SavingsGoalResponse> goals = savingsGoalService.getAllGoalsForCustomer(caller.getCustomerId());
                if (!goals.isEmpty()) {
                    SavingsGoalResponse goal = goals.get(0);
                    hasGoal = true;
                    goalName = goal.getGoalName();
                    goalProgressPercentage = goal.getProgressPercentage();
                    goalTargetAmount = goal.getTargetAmount();
                }
                limitedGoalData = false;
            } catch (RuntimeException ex) {
                limitedGoalData = true;
            }
        }

        List<SafeChatContext.KnowledgeSnippet> matchedArticles = knowledgeStore.query(rawQuery, topic, topK);

        return new SafeChatContext(accountRestricted, limitedTransactionData, limitedGoalData,
                topCategory, topCategoryAmount, totalSpend30d, hasGoal, goalName,
                goalProgressPercentage, goalTargetAmount, matchedArticles);
    }

    private SavingsGoalResponse findGoalForAccount(Long customerId, Long accountId) {
        List<SavingsGoalResponse> goals = savingsGoalService.getAllGoalsForCustomer(customerId);
        return goals.stream()
                .filter(g -> accountId.equals(g.getAccountId()))
                .findFirst()
                .orElse(null);
    }
}
