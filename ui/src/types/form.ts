import type { ProjectRewrite } from ".";

export interface ProjectFormState {
  title: string;
  description?: string;
  directory: string;
  rewrites?: ProjectRewrite[];
}
