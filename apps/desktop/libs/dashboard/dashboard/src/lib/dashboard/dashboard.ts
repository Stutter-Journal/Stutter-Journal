import { CommonModule } from '@angular/common';
import { Component, inject } from '@angular/core';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { HlmButton } from '@spartan-ng/helm/button';
import {
  lucideLayoutDashboard,
  lucidePersonStanding,
  lucideNotebookPen,
  lucideBarChart,
  lucideSettings,
} from '@ng-icons/lucide';
import {
  HlmSidebar,
  HlmSidebarContent,
  HlmSidebarGroup,
  HlmSidebarHeader,
  HlmSidebarTrigger,
  HlmSidebarWrapper,
} from '@spartan-ng/helm/sidebar';
import { HlmMenubar, HlmMenubarTrigger } from '@spartan-ng/helm/menubar';
import {
  HlmDropdownMenu,
  HlmDropdownMenuItem,
  HlmDropdownMenuSeparator,
} from '@spartan-ng/helm/dropdown-menu';
import {
  HlmCaption,
  HlmTable,
  HlmTableContainer,
  HlmTBody,
  HlmTd,
  HlmTh,
  HlmTHead,
  HlmTr,
} from '@spartan-ng/helm/table';
import { AuthClientService } from '@org/auth-data-access';
import { Logout } from '../logout/logout';
import { HlmIcon } from '@spartan-ng/helm/icon';
import { NgIcon, provideIcons } from '@ng-icons/core';

@Component({
  selector: 'lib-dashboard',
  imports: [
    HlmSidebarWrapper,
    HlmSidebar,
    HlmSidebarHeader,
    HlmSidebarContent,
    HlmSidebarGroup,
    CommonModule,
    RouterLink,
    RouterLinkActive,
    RouterOutlet,
    HlmButton,
    HlmSidebarTrigger,
    HlmMenubar,
    HlmMenubarTrigger,
    HlmDropdownMenu,
    HlmDropdownMenuItem,
    HlmDropdownMenuSeparator,
    HlmTableContainer,
    HlmTable,
    HlmCaption,
    HlmTHead,
    HlmTr,
    HlmTh,
    HlmTBody,
    HlmTd,
    Logout,
    HlmIcon,
    NgIcon,
  ],
  templateUrl: './dashboard.html',
  providers: [
    provideIcons({
      lucideLayoutDashboard,
      lucidePersonStanding,
      lucideNotebookPen,
      lucideBarChart,
      lucideSettings,
    }),
  ],
})
export class Dashboard {
  readonly auth = inject(AuthClientService);
}
