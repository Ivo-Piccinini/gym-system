import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, catchError, forkJoin, map, of, switchMap, throwError } from 'rxjs';
import { environment } from '../../environments/environment';
import { Routine, RoutineExercisePayload, RoutineLine, RoutinePayload } from './routines.models';
import { toApiDate } from './routine.utils';

/** La rutina se creó/actualizó pero alguno de sus ejercicios no se pudo guardar. */
export class PartialSaveError extends Error {
  constructor(
    readonly routineId: string,
    readonly detail: unknown
  ) {
    super('Algunos ejercicios no se pudieron guardar.');
  }
}

@Injectable({
  providedIn: 'root'
})
export class RoutineService {
  private readonly http = inject(HttpClient);
  // environment.apiUrl ya incluye "/api"
  private readonly routinesUrl = `${environment.apiUrl}/routines`;
  private readonly linesUrl = `${environment.apiUrl}/full-routines`;

  // Mientras el back no tenga baja lógica de rutinas, las dadas de baja se ocultan en este navegador.
  private readonly HIDDEN_KEY = 'gym_hidden_routines';

  getRoutines(): Observable<Routine[]> {
    return this.http
      .get<Routine[]>(this.routinesUrl)
      .pipe(map((routines) => routines.filter((r) => !this.getHiddenIds().includes(r.publicId))));
  }

  getRoutine(publicId: string): Observable<Routine> {
    return this.http.get<Routine>(`${this.routinesUrl}/${encodeURIComponent(publicId)}`);
  }

  /** Alta: crea la rutina y después carga cada ejercicio en /api/full-routines. */
  createRoutine(payload: RoutinePayload): Observable<Routine> {
    return this.http.post<Routine>(this.routinesUrl, this.toRoutineBody(payload)).pipe(
      switchMap((created) =>
        this.runAll(payload.exercises.map((item) => this.createLine(created.publicId, item))).pipe(
          map(() => created),
          catchError((err) => throwError(() => new PartialSaveError(created.publicId, err)))
        )
      )
    );
  }

  /**
   * Edición: actualiza la rutina y sincroniza los ejercicios contra los que ya tenía
   * (los nuevos se crean, los existentes se actualizan y los que se quitaron se dan de baja).
   */
  updateRoutine(publicId: string, payload: RoutinePayload, originalLines: RoutineLine[]): Observable<Routine> {
    const keptIds = new Set(payload.exercises.map((e) => e.publicId).filter(Boolean));

    const ops = [
      ...payload.exercises.map((item) =>
        item.publicId ? this.updateLine(item.publicId, item) : this.createLine(publicId, item)
      ),
      ...originalLines.filter((line) => !keptIds.has(line.publicId)).map((line) => this.deleteLine(line.publicId))
    ];

    return this.http
      .put<Routine>(`${this.routinesUrl}/${encodeURIComponent(publicId)}`, {
        ...this.toRoutineBody(payload),
        publicId
      })
      .pipe(
        switchMap((updated) =>
          this.runAll(ops).pipe(
            map(() => updated),
            catchError((err) => throwError(() => new PartialSaveError(publicId, err)))
          )
        )
      );
  }

  /** Baja: se dan de baja sus ejercicios (baja lógica del back) y la rutina deja de listarse. */
  deleteRoutine(routine: Routine): Observable<void> {
    const ops = (routine.exercises ?? []).map((line) => this.deleteLine(line.publicId));
    return this.runAll(ops).pipe(
      map(() => {
        this.hide(routine.publicId);
      })
    );
  }

  // ---------------------------------------------------------------- helpers

  private toRoutineBody(payload: RoutinePayload) {
    return {
      name: payload.name,
      clientId: payload.clientId,
      professorId: payload.professorId,
      typeRoutine: payload.typeRoutine,
      startDate: toApiDate(payload.startDate),
      endDate: payload.endDate ? toApiDate(payload.endDate) : null
    };
  }

  private createLine(routineId: string, item: RoutineExercisePayload): Observable<unknown> {
    return this.http.post(this.linesUrl, {
      routineID: routineId,
      exerciseID: item.exerciseId,
      series: item.series,
      reps: item.reps,
      weight: item.weight
    });
  }

  private updateLine(lineId: string, item: RoutineExercisePayload): Observable<unknown> {
    return this.http.put(`${this.linesUrl}/${encodeURIComponent(lineId)}`, {
      exerciseID: item.exerciseId,
      series: item.series,
      reps: item.reps,
      weight: item.weight
    });
  }

  private deleteLine(lineId: string): Observable<unknown> {
    return this.http.delete(`${this.linesUrl}/${encodeURIComponent(lineId)}`);
  }

  private runAll(ops: Observable<unknown>[]): Observable<unknown> {
    return ops.length > 0 ? forkJoin(ops) : of(null);
  }

  private getHiddenIds(): string[] {
    try {
      return JSON.parse(localStorage.getItem(this.HIDDEN_KEY) ?? '[]') as string[];
    } catch {
      return [];
    }
  }

  private hide(publicId: string): void {
    localStorage.setItem(this.HIDDEN_KEY, JSON.stringify([...this.getHiddenIds(), publicId]));
  }
}