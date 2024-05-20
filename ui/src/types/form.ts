import type { ProjectRewrite } from ".";

export interface ProjectFormState {
  title: string;
  icon?: string;
  description?: string;
  directory: string;
  rewrites?: ProjectRewrite[];
}
