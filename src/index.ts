import { registerPlugin } from '@capacitor/core';

import type { CapacitorGalleryPlugin } from './definitions';

const CapacitorGallery = registerPlugin<CapacitorGalleryPlugin>(
  'CapacitorGallery',
  {
    web: () => import('./web').then(m => new m.CapacitorGalleryWeb()),
  },
);

export * from './definitions';
export { CapacitorGallery };
