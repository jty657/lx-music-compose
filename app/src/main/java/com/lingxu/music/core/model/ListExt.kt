package com.lingxu.music.core.model

/**
 * 翻页去重追加：保留当前列表，把 [next] 中 id 未出现过的项追加到末尾（保留首次出现顺序）。
 *
 * 此前多个列表页各自写 `list + result.filterNot { it.id in list.map { x -> x.id } }`：
 * 一是三处重复，二是 `list.map { ... }` 在 filterNot 的谓词里，每比较一项就重建一次 id 集合（O(N·M)）。
 * 统一到这里后 id 集合只构建一次（O(N+M)）；同时用 `seen.add` 顺带去掉新批次内部的重复 id，
 * 避免重复 key 让 LazyColumn 崩溃（本项目多处依赖 id 作为列表 key 的唯一性）。
 */
inline fun <T> List<T>.appendDistinctById(
    next: List<T>,
    crossinline idOf: (T) -> String,
): List<T> {
    if (next.isEmpty()) return this
    val seen = this.mapTo(mutableSetOf(), idOf)
    return this + next.filter { seen.add(idOf(it)) }
}
