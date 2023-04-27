export interface CapacitorGalleryPlugin {
  getGalleryItems(options: GetGalleryItemsOptions): Promise<GetGalleryItemsResponse>;
}

export interface GetGalleryItemsOptions {
  quantity?: number;
  offset?: number
}

export interface GalleryItem {
  id: string;
  base64Image: string;
  creationDate: string;
}

export interface GetGalleryItemsResponse {
  count: number,
  results: GalleryItem [],
  nextOffset: number,
  nextMaxQuantity: number
}