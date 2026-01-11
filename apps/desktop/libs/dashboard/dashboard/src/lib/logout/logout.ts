import { Component, inject } from '@angular/core';
import { Router } from '@angular/router';
import { HlmButton } from '@spartan-ng/helm/button';
import { HlmSidebarFooter } from '@spartan-ng/helm/sidebar';
import { AuthClientService } from '@org/auth-data-access';
import { toast } from 'ngx-sonner';

@Component({
  selector: 'lib-logout',
  standalone: true,
  imports: [HlmButton, HlmSidebarFooter],
  templateUrl: './logout.html',
  styleUrl: './logout.css',
})
export class Logout {
  readonly auth = inject(AuthClientService);
  private readonly router = inject(Router);

  async logout() {
    await this.auth.logout();
    toast.success('You have been logged out');
    await this.router.navigateByUrl('/landing');
  }
}
