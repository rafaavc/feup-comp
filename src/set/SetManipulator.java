package set;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SetManipulator {
    public static <T> Set<T> difference(Set<T> set1, Set<T> set2) {
        Set<T> res = new HashSet<>(set1);
        res.removeAll(set2);
        return res;
    }

    public static <T> Set<T> union(Set<T> set1, Set<T> set2) {
        Set<T> res = new HashSet<>(set1);
        res.addAll(set2);
        return res;
    }

    public static <T> boolean equal(Set<T> set1, Set<T> set2) {
        return set1.containsAll(set2);
    }

    public static <T> boolean equal(List<Set<T>> l1, List<Set<T>> l2) {
        if (l1.size() != l2.size()) return false;
        for (int i = 0; i < l1.size(); i++) {
            if (!equal(l1.get(i), l2.get(i))) return false;
        }
        return true;
    }

    public static <T> List<Set<T>> bidimensionalCopy(List<Set<T>> list) {
        List<Set<T>> res = new ArrayList<>();
        for (Set<T> set : list)
            res.add(new HashSet<>(set));
        return res;
    }
}
