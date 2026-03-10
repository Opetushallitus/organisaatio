import React from 'react';
import { Icon, IconProps } from '@iconify/react';

export default function IconWrapper(props: IconProps) {
    const { ref: _, ...rest } = props;
    return (
        <Icon fr={0} {...rest}>
            {props.children}
        </Icon>
    );
}
