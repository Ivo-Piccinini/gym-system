import { Component, computed, forwardRef, input, signal } from '@angular/core';
import { ControlValueAccessor, NG_VALUE_ACCESSOR } from '@angular/forms';

const MINUTE_CHIPS = ['00', '15', '30', '45'];

/**
 * Campo de horario compacto (24 horas) para formularios reactivos.
 * Se escribe la hora (0-23) y se tocan los minutos. El valor del control es "HH:mm" o "".
 */
@Component({
  selector: 'app-class-time-field',
  standalone: true,
  templateUrl: './class-time-field.html',
  styleUrls: ['./class-time-field.css'],
  providers: [
    {
      provide: NG_VALUE_ACCESSOR,
      useExisting: forwardRef(() => ClassTimeField),
      multi: true
    }
  ]
})
export class ClassTimeField implements ControlValueAccessor {
  /** id del campo de hora, para que el <label for> del formulario lo enfoque. */
  readonly inputId = input<string>('');
  readonly invalid = input<boolean>(false);

  protected readonly hour = signal<string>('');
  protected readonly minute = signal<string>('');
  protected readonly disabled = signal<boolean>(false);

  /** 00, 15, 30 y 45; si el valor tiene otros minutos (ej. una clase vieja) se suman. */
  protected readonly minuteOptions = computed(() => {
    const options = [...MINUTE_CHIPS];
    const current = this.minute();
    if (current && !options.includes(current)) {
      options.push(current);
    }
    return options.sort();
  });

  private onChange: (value: string) => void = () => undefined;
  private onTouched: () => void = () => undefined;

  // --- ControlValueAccessor ---
  writeValue(value: string | null): void {
    const match = /^(\d{1,2}):(\d{2})/.exec(value ?? '');
    this.hour.set(match ? match[1].padStart(2, '0') : '');
    this.minute.set(match ? match[2] : '');
  }

  registerOnChange(fn: (value: string) => void): void {
    this.onChange = fn;
  }

  registerOnTouched(fn: () => void): void {
    this.onTouched = fn;
  }

  setDisabledState(isDisabled: boolean): void {
    this.disabled.set(isDisabled);
  }

  // --- eventos ---
  protected onHourInput(event: Event): void {
    const input = event.target as HTMLInputElement;
    let digits = input.value.replace(/\D/g, '').slice(0, 2);
    if (digits !== '' && Number(digits) > 23) {
      digits = '23';
    }
    input.value = digits;
    this.hour.set(digits);
    if (digits && !this.minute()) {
      this.minute.set('00');
    }
    this.emit();
  }

  protected onHourBlur(): void {
    if (this.hour().length === 1) {
      this.hour.set(this.hour().padStart(2, '0'));
      this.emit();
    }
    this.onTouched();
  }

  protected stepHour(delta: number, event: Event): void {
    event.preventDefault();
    const next = (Number(this.hour() || '0') + delta + 24) % 24;
    this.hour.set(String(next).padStart(2, '0'));
    if (!this.minute()) {
      this.minute.set('00');
    }
    this.emit();
  }

  protected setMinute(minute: string): void {
    this.minute.set(minute);
    this.emit();
    this.onTouched();
  }

  private emit(): void {
    const hour = this.hour();
    const minute = this.minute();
    this.onChange(hour && minute ? `${hour.padStart(2, '0')}:${minute}` : '');
  }
}
