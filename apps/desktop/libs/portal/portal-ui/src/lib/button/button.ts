import { NgIf } from '@angular/common';
import { Component, Input } from '@angular/core';

@Component({
  selector: 'lib-button',
  standalone: true,
  imports: [NgIf],
  templateUrl: './button.html',
  styleUrl: './button.css',
})
export class ButtonComponent {
  @Input() variant: 'primary' | 'secondary' | 'ghost' = 'primary';
  @Input() size: 'sm' | 'md' | 'lg' = 'md';
  @Input() loading = false;
  @Input() disabled = false;
  @Input() type: 'button' | 'submit' | 'reset' = 'button';

  get classes(): string {
    const base =
      'inline-flex items-center justify-center gap-2 rounded-xl font-medium transition focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-neutral-900 focus-visible:ring-offset-2 focus-visible:ring-offset-white disabled:cursor-not-allowed disabled:opacity-50';
    const variants = {
      primary: 'bg-neutral-900 text-white shadow-[var(--shadow-sm)] hover:bg-neutral-800',
      secondary:
        'bg-neutral-100 text-neutral-900 ring-1 ring-neutral-200 hover:bg-neutral-200',
      ghost: 'bg-transparent text-neutral-700 hover:bg-neutral-100',
    } as const;
    const sizes = {
      sm: 'h-8 px-3 text-xs',
      md: 'h-10 px-4 text-sm',
      lg: 'h-12 px-5 text-base',
    } as const;

    return [base, variants[this.variant], sizes[this.size]].join(' ');
  }
}
