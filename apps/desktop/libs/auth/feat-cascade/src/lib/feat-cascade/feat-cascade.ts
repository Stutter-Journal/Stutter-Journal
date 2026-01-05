import { ChangeDetectionStrategy, Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { CardModule } from 'primeng/card';
import { TabsModule } from 'primeng/tabs';
import { InputTextModule } from 'primeng/inputtext';
import { PasswordModule } from 'primeng/password';
import { ButtonModule } from 'primeng/button';
import { DividerModule } from 'primeng/divider';
import { CheckboxModule } from 'primeng/checkbox';
import { CascadeSelectModule } from 'primeng/cascadeselect';
import { Select } from 'primeng/select';

@Component({
  selector: 'lib-feat-cascade',
  imports: [
    CommonModule,
    ReactiveFormsModule,
    CardModule,
    TabsModule,
    InputTextModule,
    PasswordModule,
    ButtonModule,
    DividerModule,
    CheckboxModule,
    CascadeSelectModule,
    Select,
  ],
  templateUrl: './feat-cascade.html',
  styleUrl: './feat-cascade.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class FeatCascade {
  private readonly fb = inject(FormBuilder);

  readonly loginForm = this.fb.group({
    email: ['', [Validators.required, Validators.email]],
    password: ['', [Validators.required]],
    remember: [true],
  });

  readonly registerForm = this.fb.group({
    fullName: ['', [Validators.required]],
    email: ['', [Validators.required, Validators.email]],
    password: ['', [Validators.required, Validators.minLength(8)]],
    country: [null, [Validators.required]],
    accept: [false, [Validators.requiredTrue]],
  });

  readonly countryGroups = [
    {
      name: 'Americas',
      code: 'AM',
      items: [
        { name: 'United States', code: 'US' },
        { name: 'Canada', code: 'CA' },
        { name: 'Mexico', code: 'MX' },
      ],
    },
    {
      name: 'Europe',
      code: 'EU',
      items: [
        { name: 'Germany', code: 'DE' },
        { name: 'United Kingdom', code: 'GB' },
        { name: 'France', code: 'FR' },
      ],
    },
    {
      name: 'APAC',
      code: 'AP',
      items: [
        { name: 'Australia', code: 'AU' },
        { name: 'Singapore', code: 'SG' },
        { name: 'Japan', code: 'JP' },
      ],
    },
  ];

  submitLogin(): void {
    if (this.loginForm.invalid) {
      this.loginForm.markAllAsTouched();
      return;
    }
    // eslint-disable-next-line no-console
    console.log('Login payload', this.loginForm.value);
  }

  submitRegister(): void {
    if (this.registerForm.invalid) {
      this.registerForm.markAllAsTouched();
      return;
    }
    // eslint-disable-next-line no-console
    console.log('Register payload', this.registerForm.value);
  }
}
