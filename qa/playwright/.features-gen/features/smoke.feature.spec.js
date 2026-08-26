// Generated from: features\smoke.feature
import { test } from "playwright-bdd";

test.describe('Smoke check', () => {

  test('Home page responds', async ({ Given, Then, page }) => { 
    await Given('I open the application home page', null, { page }); 
    await Then('the page title should not be empty', null, { page }); 
  });

});

// == technical section ==

test.use({
  $test: [({}, use) => use(test), { scope: 'test', box: true }],
  $uri: [({}, use) => use('features\\smoke.feature'), { scope: 'test', box: true }],
  $bddFileData: [({}, use) => use(bddFileData), { scope: "test", box: true }],
});

const bddFileData = [ // bdd-data-start
  {"pwTestLine":6,"pickleLine":2,"tags":[],"steps":[{"pwStepLine":7,"gherkinStepLine":3,"keywordType":"Context","textWithKeyword":"Given I open the application home page","stepMatchArguments":[]},{"pwStepLine":8,"gherkinStepLine":4,"keywordType":"Outcome","textWithKeyword":"Then the page title should not be empty","stepMatchArguments":[]}]},
]; // bdd-data-end