import { ChangeDetectionStrategy, Component, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { CardModule } from 'primeng/card';
import { TabsModule } from 'primeng/tabs';
import { FeatLogin } from '@org/feat-login';
import { FeatRegister } from '@org/feat-register';

type Mode = 'login' | 'register';

@Component({
  selector: 'lib-feat-cascade',
  imports: [CommonModule, CardModule, TabsModule, FeatLogin, FeatRegister],
  templateUrl: './feat-cascade.html',
  styleUrl: './feat-cascade.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class FeatCascade {
  readonly mode = signal<Mode>('login');
}
