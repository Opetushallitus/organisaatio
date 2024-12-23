import * as React from 'react';
import sad from '../img/sad.png';
import styles from './VirheSivu.module.css';

type Props = {
    children: React.ReactNode;
};

export default function VirheSivu(props: Props) {
    return (
        <div className={styles.VirheKirjautunut}>
            <div className={styles.VirheKirjautunutTausta}>
                <div>
                    <img src={sad} alt="" />
                </div>
                {props.children}
            </div>
        </div>
    );
}
