import { effect, Injectable, signal } from '@angular/core';

export type ThemeType = 'dark' | 'light';

@Injectable({
  providedIn: 'root',
})
export class Theme {
  private readonly THEME_KEY = 'voltio_theme';

  currentTheme = signal<ThemeType>(this.getInitialTheme());

  constructor() {
    effect(() => {
      const theme: string = this.currentTheme();
      localStorage.setItem(this.THEME_KEY, theme);

      if (theme === 'dark') {
        document.documentElement.classList.add('dark-theme');
        document.documentElement.classList.remove('light-theme');
      } else {
        document.documentElement.classList.add('light-theme');
        document.documentElement.classList.remove('dark-theme');
      }
    });
  }

  toggleTheme(): void {
    this.currentTheme.update((prev) => (prev === 'dark' ? 'light' : 'dark'));
  }

  private getInitialTheme(): ThemeType {
    const saved = localStorage.getItem(this.THEME_KEY);
    if (saved === 'dark' || saved === 'light') {
      return saved as ThemeType;
    }
    return window.matchMedia('(prefers-color-scheme: light)').matches ? 'light' : 'dark';
  }
}