import { ChangeDetectionStrategy, Component, Input } from '@angular/core';
import { HlmToaster } from '@spartan-ng/helm/sonner';
import type { ToasterProps } from 'ngx-sonner';

@Component({
  selector: 'lib-toast',
  standalone: true,
  imports: [HlmToaster],
  templateUrl: './toast.html',
  styleUrls: ['./toast.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class Toast {
  @Input() theme: ToasterProps['theme'] = 'light';
  @Input() position: ToasterProps['position'] = 'bottom-right';
  @Input() duration: ToasterProps['duration'] = 4000;
  @Input() visibleToasts: ToasterProps['visibleToasts'] = 3;
  @Input() richColors: ToasterProps['richColors'] = false;
  @Input() expand: ToasterProps['expand'] = false;
  @Input() closeButton: ToasterProps['closeButton'] = false;
  @Input() invert: ToasterProps['invert'] = false;
  @Input() hotKey: ToasterProps['hotkey'] = ['altKey', 'KeyT'];
  @Input() toastOptions: ToasterProps['toastOptions'] = {};
  @Input() offset: ToasterProps['offset'] = null;
  @Input() dir: ToasterProps['dir'] = 'auto';
}
