import { CanActivateFn, Router } from '@angular/router';
import { inject } from '@angular/core';
import { Role } from '../models/api.models';
import { AuthService } from '../services/auth.service';

export const roleGuard = (requiredRole: Role): CanActivateFn => () => {
  const auth = inject(AuthService); const router = inject(Router);
  return auth.role() === requiredRole ? true : router.createUrlTree([auth.role() === 'TEACHER' ? '/teacher/dashboard' : '/student/dashboard']);
};
