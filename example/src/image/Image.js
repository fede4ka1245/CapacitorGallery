import React from 'react';
import styles from './Image.module.scss';

const Image = ({ src }) => {
  return (
    <div className={styles.main}>
      <img alt={'gallery-item'} src={src} />
    </div>
  );
};

export default Image;