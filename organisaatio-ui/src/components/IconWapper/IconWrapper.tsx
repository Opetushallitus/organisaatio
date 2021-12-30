import React from 'react';
import { Icon } from '@iconify/react';

export default function IconWrapper(props, children) {
    return <Icon {...props}>{children}</Icon>;
}
