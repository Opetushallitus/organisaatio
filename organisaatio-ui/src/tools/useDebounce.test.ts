import { renderHook } from '@testing-library/react-hooks';
import useDebounce from './useDebounce';

describe('useDebounce', () => {
    it('should update debounced value after delay passes', (done) => {
        const { result, rerender } = renderHook(({ searchStr, delay = 1000 }) => useDebounce(searchStr, delay), {
            initialProps: { searchStr: '', delay: 1000 } as { searchStr: string; delay?: number },
        });

        rerender({ searchStr: 'moi' });
        rerender({ searchStr: 'moik' });
        rerender({ searchStr: 'moikka' });
        expect(result.current).toEqual('');
        setTimeout(() => {
            expect(result.current).toEqual('moikka');
            done();
        }, 1001);
    });
    it('should update debounced value based on updated delay also', (done) => {
        const { result, rerender } = renderHook(({ searchStr, delay = 1000 }) => useDebounce(searchStr, delay), {
            initialProps: { searchStr: '', delay: 1000 } as { searchStr: string; delay?: number },
        });
        rerender({ searchStr: 'moikka', delay: 350 });
        rerender({ searchStr: 'moikkamoi1', delay: 350 });
        expect(result.current).toEqual('');
        setTimeout(() => {
            expect(result.current).toEqual('moikkamoi1');
            done();
        }, 352);
    });
});
