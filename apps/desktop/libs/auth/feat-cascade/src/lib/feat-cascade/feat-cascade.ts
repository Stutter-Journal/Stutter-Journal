import { ChangeDetectionStrategy, Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { CardModule } from 'primeng/card';
import { TabsModule } from 'primeng/tabs';
import { FeatLogin } from '@org/feat-login';
import { FeatRegister } from '@org/feat-register';
import { StyleClass } from 'primeng/styleclass';

@Component({
  selector: 'lib-feat-cascade',
  imports: [
    CommonModule,
    CardModule,
    TabsModule,
    FeatLogin,
    FeatRegister,
    StyleClass,
  ],
  templateUrl: './feat-cascade.html',
  styleUrl: './feat-cascade.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class FeatCascade {}
