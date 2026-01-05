import { Component } from '@angular/core';
import { RouterModule } from '@angular/router';
import { NxWelcome } from './nx-welcome';
import { Select } from 'primeng/select';
import { FormsModule } from '@angular/forms';

@Component({
  imports: [NxWelcome, RouterModule, Select, FormsModule],
  selector: 'app-root',
  templateUrl: './app.html',
  styleUrl: './app.css',
})
export class App {
  protected title = 'portal';
}
