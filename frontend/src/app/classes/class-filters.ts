import { Component, input, model } from '@angular/core';
import { ApiDay, DAY_LABELS, DAY_ORDER } from './classes.models';

@Component({
  selector: 'app-class-filters',
  standalone: true,
  templateUrl: './class-filters.html',
  styleUrls: ['./class-filters.css']
})
export class ClassFilters {
  /** Disciplinas disponibles; salen de los datos del back, no están hardcodeadas. */
  readonly disciplines = input<string[]>([]);

  /** Two-way: [(discipline)] y [(day)] desde el padre. '' = todas / todos. */
  readonly discipline = model<string>('');
  readonly day = model<ApiDay | ''>('');

  protected readonly days = DAY_ORDER.map((value) => ({
    value,
    label: DAY_LABELS[value],
    short: DAY_LABELS[value].slice(0, 3)
  }));

  protected onDisciplineChange(event: Event): void {
    this.discipline.set((event.target as HTMLSelectElement).value);
  }

  protected setDay(day: ApiDay | ''): void {
    this.day.set(day);
  }

  protected clear(): void {
    this.discipline.set('');
    this.day.set('');
  }

  protected hasActiveFilters(): boolean {
    return !!this.discipline() || !!this.day();
  }
}
