import { Directive } from '@angular/core';
import { classes } from '@spartan-ng/helm/utils';

@Directive({
  // eslint-disable-next-line @angular-eslint/directive-selector
  selector: 'hlm-select, brn-select [hlm]',
})
export class HlmSelect {
  constructor() {
    classes(() => 'space-y-2');
  }
}
