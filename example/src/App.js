import React, { useEffect, useState } from 'react';
import { CapacitorGallery } from 'capacitor-gallery';
import Image from "./image/Image";
import ImageLoader from "./imageLoader/ImageLoader";
import {useInView} from "react-intersection-observer";

function App() {
  const [data, setData] = useState({
    count: 0,
    results: [],
    nextOffset: 0,
    nextMaxQuantity: 20
  });
  const [isLoading, setIsLoading] = useState(false);
  const { ref, inView } = useInView({
    threshold: 0.25,
  });

  useEffect(() => {
    if (!inView || isLoading || data.nextMaxQuantity === 0) {
      return;
    }

    setIsLoading(true);
    CapacitorGallery.getGalleryItems({
      quantity: data.nextMaxQuantity > 20 ? 20 : data.nextMaxQuantity,
      offset: data.nextOffset
    })
      .then((res) => {
        setData({
          ...res,
          results: [
            ...data.results,
            ...res.results
          ]
        })
      })
      .finally(() => {
        setIsLoading(false);
      });
  }, [inView]);

  return (
    <>
      <header className="app-header">
        <div className={'app-header-text'}>
          Gallery App
        </div>
      </header>
      <div className="app">
        <section className="app-body">
          {data.results.map(({ base64Image }, index) => (
            <Image src={base64Image} key={index} />
          ))}
          {isLoading && Array.from({ length: 12 }).map((_, index) => (
            <ImageLoader key={index} />
          ))}
          <div ref={ref} />
        </section>
      </div>
    </>
  );
}

export default App;
