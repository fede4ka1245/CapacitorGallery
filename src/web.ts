import { WebPlugin } from '@capacitor/core';

import type {CapacitorGalleryPlugin, GetGalleryItemsOptions, GetGalleryItemsResponse} from './definitions';

export class CapacitorGalleryWeb
  extends WebPlugin
  implements CapacitorGalleryPlugin
{
  async getGalleryItems(options?: GetGalleryItemsOptions): Promise<GetGalleryItemsResponse> {
    console.log('getGalleryItems', options);
    throw this.unimplemented('Not implemented on web.');
  }
}
