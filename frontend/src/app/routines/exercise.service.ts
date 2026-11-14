import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { Exercise, MuscleGroup } from './routines.models';

@Injectable({
  providedIn: 'root'
})
export class ExerciseService {
  private readonly http = inject(HttpClient);
  // environment.apiUrl ya incluye "/api"
  private readonly baseUrl = `${environment.apiUrl}/exercises`;

  /**
   * Catálogo de ejercicios maestros habilitados.
   * @param muscleGroup si se informa, el back devuelve solo los de ese grupo muscular.
   */
  getExercises(muscleGroup?: MuscleGroup): Observable<Exercise[]> {
    let params = new HttpParams();
    if (muscleGroup) {
      params = params.set('muscleGroup', muscleGroup);
    }
    return this.http.get<Exercise[]>(this.baseUrl, { params });
  }
}