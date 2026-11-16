import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { ClassPayload, GymClass } from './classes.models';

@Injectable({
  providedIn: 'root'
})
export class ClassAdminService {
  private readonly http = inject(HttpClient);
  // environment.apiUrl ya incluye "/api"
  private readonly baseUrl = `${environment.apiUrl}/gym-classes`;

  /** Alta de clase (ADMIN y PROFESSOR). */
  createClass(payload: ClassPayload): Observable<GymClass> {
    return this.http.post<GymClass>(this.baseUrl, payload);
  }

  /** Edición de clase: el back solo actualiza día, horario y cupo máximo. */
  updateClass(externalId: string, payload: ClassPayload): Observable<GymClass> {
    return this.http.put<GymClass>(`${this.baseUrl}/${externalId}`, payload);
  }

  /** Baja lógica. El back la rechaza si la clase tiene alumnos inscriptos. */
  deleteClass(externalId: string): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${externalId}`);
  }
}
