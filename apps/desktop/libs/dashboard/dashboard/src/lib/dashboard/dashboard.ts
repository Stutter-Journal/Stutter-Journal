import { Component, inject } from '@angular/core';
import { RouterLink, RouterLinkActive } from '@angular/router';
import { HlmButton } from '@spartan-ng/helm/button';
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

@Component({
  selector: 'lib-dashboard',
  imports: [
    HlmSidebarWrapper,
    HlmSidebar,
    HlmSidebarHeader,
    HlmSidebarContent,
    HlmSidebarGroup,
    RouterLink,
    RouterLinkActive,
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
  ],
  templateUrl: './dashboard.html',
  styleUrl: './dashboard.css',
})
export class Dashboard {
  readonly auth = inject(AuthClientService);
}
