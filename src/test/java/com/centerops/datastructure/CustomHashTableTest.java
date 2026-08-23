package com.centerops.datastructure;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CustomHashTableTest {

    @Test
    void storesUpdatesAndRemovesValues() {
        CustomHashTable<String, Integer> table = new CustomHashTable<>();

        assertThat(table.put("person-1", 1)).isNull();
        assertThat(table.put("person-1", 2)).isEqualTo(1);
        assertThat(table.get("person-1")).isEqualTo(2);
        assertThat(table.containsKey("person-1")).isTrue();
        assertThat(table.remove("person-1")).isEqualTo(2);
        assertThat(table.containsKey("person-1")).isFalse();
        assertThat(table).extracting(CustomHashTable::size).isEqualTo(0);
    }

    @Test
    void preservesEntriesAfterResizeAndHashCollisions() {
        CustomHashTable<CollisionKey, Integer> table = new CustomHashTable<>(2);

        for (int index = 0; index < 40; index++) {
            table.put(new CollisionKey(index), index);
        }

        assertThat(table.size()).isEqualTo(40);
        for (int index = 0; index < 40; index++) {
            assertThat(table.get(new CollisionKey(index))).isEqualTo(index);
        }
    }

    private record CollisionKey(int id) {
        @Override
        public int hashCode() {
            return 7;
        }
    }
}
