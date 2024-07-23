import { axiosInstance } from '@halo-dev/api-client';
import { ConsoleApiStaticpageHaloRunV1alpha1ProjectApi, ProjectV1alpha1Api } from './generated';

const staticPageCoreApiClient = {
  project: new ProjectV1alpha1Api(undefined, '', axiosInstance),
};

const staticPageConsoleApiClient = {
  project: new ConsoleApiStaticpageHaloRunV1alpha1ProjectApi(undefined, '', axiosInstance),
};

export { staticPageConsoleApiClient, staticPageCoreApiClient };
