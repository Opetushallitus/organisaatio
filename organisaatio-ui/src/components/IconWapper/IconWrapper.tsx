import React from 'react';
import { Icon } from '@iconify/react';

export default function IconWrapper(props) {
    return (
        <Icon fr={0} {...props}>
            {props.children}
        </Icon>
    );
}
