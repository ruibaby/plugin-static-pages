import type { Metadata } from "@halo-dev/api-client";

export interface Project {
  spec: Spec;
  apiVersion: "staticpage.halo.run/v1alpha1";
  kind: "Project";
  metadata: Metadata;
}

export interface Spec {
  title: string;
  icon?: string;
  description?: string;
  directory: string;
  rewrites?: ProjectRewrite[];
}

export interface ListResponse<T> {
  page: number;
  size: number;
  total: number;
  items: T[];
  first: boolean;
  last: boolean;
  hasNext: boolean;
  hasPrevious: boolean;
  totalPages: number;
}

export interface ProjectFile {
  name: string;
  path: string;
  size: number;
  type: string;
  lastModifiedTime: string;
  canRead: boolean;
  canWrite: boolean;
  directory: boolean;
}

export interface ProjectRewrite {
  source: string;
  target: string;
}
