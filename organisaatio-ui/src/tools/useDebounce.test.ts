import { renderHook } from '@testing-library/react-hooks';
import useDebounce from './useDebounce';

describe('useDebounce', () => {
    it('should update debounced value after delay passes', () => {
        const { result, rerender } = renderHook(({ searchStr, delay = 1000 }) => useDebounce(searchStr, delay), {
            initialProps: { searchStr: '', delay: 1000 } as { searchStr: string; delay?: number },
        });

        rerender({ searchStr: 'moi' });
        rerender({ searchStr: 'moik' });
        rerender({ searchStr: 'moikka' });
        expect(result.current).toEqual('');
        setTimeout(() => {
            rerender({ searchStr: 'moikkamoi' });
            expect(result.current).toEqual('moikkamoi');
        }, 1001);
    });
    it('should update debounced value based on updated delay also', () => {
        const { result, rerender } = renderHook(({ searchStr, delay = 1000 }) => useDebounce(searchStr, delay), {
            initialProps: { searchStr: '', delay: 1000 } as { searchStr: string; delay?: number },
        });
        expect(result.current).toEqual('');
        rerender({ searchStr: 'moikka', delay: 350 });
        rerender({ searchStr: 'moikkamoi1' });
        setTimeout(() => {
            rerender({ searchStr: 'moikkamoi' });
            expect(result.current).toEqual('moikkamoi');
        }, 351);
    });
});
