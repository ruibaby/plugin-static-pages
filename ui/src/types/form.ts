import type { ProjectRewrite } from '@/api/generated';

export interface ProjectFormState {
  title: string;
  icon?: string;
  description?: string;
  directory: string;
  rewrites?: ProjectRewrite[];
}
