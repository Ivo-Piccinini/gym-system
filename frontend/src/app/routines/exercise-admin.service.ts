import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { ExercisePayload } from './exercise-bank.models';
import { Exercise } from './routines.models';

@Injectable({
  providedIn: 'root'
})
export class ExerciseAdminService {
  private readonly http = inject(HttpClient);
  // environment.apiUrl ya incluye "/api"
  private readonly baseUrl = `${environment.apiUrl}/exercises`;

  /** Alta de ejercicio (ADMIN y PROFESSOR). El back responde 409 si el nombre ya existe. */
  createExercise(payload: ExercisePayload): Observable<Exercise> {
    return this.http.post<Exercise>(this.baseUrl, payload);
  }

  /** Edición de ejercicio. */
  updateExercise(idPublic: string, payload: ExercisePayload): Observable<Exercise> {
    return this.http.put<Exercise>(`${this.baseUrl}/${idPublic}`, payload);
  }

  /** Baja lógica: el ejercicio deja de ofrecerse para armar rutinas. */
  deleteExercise(idPublic: string): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${idPublic}`);
  }
}
