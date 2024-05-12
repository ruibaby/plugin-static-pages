import type { Secret } from ".";

export interface ProjectFormState {
  title: string;
  description?: string;
  directory: string;
  secret: Secret;
}
