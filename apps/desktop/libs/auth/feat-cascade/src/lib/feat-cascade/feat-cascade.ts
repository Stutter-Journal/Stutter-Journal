import {
  ChangeDetectionStrategy,
  Component,
  EventEmitter,
  Output,
  signal,
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { FeatLogin } from '@org/feat-login';
import { FeatRegister } from '@org/feat-register';

type Mode = 'login' | 'register';

@Component({
  selector: 'lib-feat-cascade',
  imports: [CommonModule, FeatLogin, FeatRegister],
  templateUrl: './feat-cascade.html',
  styleUrl: './feat-cascade.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class FeatCascade {
  @Output() authed = new EventEmitter<void>();
  readonly mode = signal<Mode>('login');
}
