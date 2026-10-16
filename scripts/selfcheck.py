#!/usr/bin/env python3
"""聆序项目静态自检：在无法本地编译的前提下，尽量拦住云端编译才会暴露的低级错误。

检查项（都是本项目实际踩过的坑）：
1. import 行必须整行匹配 ^import [\\w.]+$ 且行内只有一个 import 关键字（防 import 粘连）
2. MiuixIcons.X 使用前必须 import top.yukonga.miuix.kmp.icon.extended.X（踩过 Layers 两次）
3. Compose foundation.layout 的 Spacer/Column/Row/Box 用了就必须 import（踩过 Spacer）
4. { } 与 ( ) 数量平衡（防编辑残留导致的方法重复/括号不平衡）
5. 包名笔误 com.lingxu.mobile / muii
6. 把赋值写成函数调用：= false() / = true()
7. Card 等 Miuix 组件同时带 onClick 与 combinedClickable（点击区域冲突）
8. MusicInfo / MusicMeta 的字段访问必须真实存在（本项目音质字段在 MusicInfo 顶层，
   不在 meta 里；原版则在 meta.qualitys，极易写错位置）
9. 项目内 import 的顶层符号必须真实存在（拦「组件被误删但调用方还在 import」）
10. 重复的 @Composable 注解（同函数写两遍会报 not repeatable）
"""
import pathlib
import re
import sys

ROOT = pathlib.Path(__file__).resolve().parent.parent
BASE = ROOT / "app/src/main/java"

LAYOUT_SYMBOLS = {"Spacer", "Column", "Row", "Box"}

# Compose 扩展函数：用了就必须 import（云端编译才报）
COMPOSE_EXTENSIONS = {
    "collectAsState": "androidx.compose.runtime.collectAsState",
    "clickable": "androidx.compose.foundation.clickable",
    "combinedClickable": "androidx.compose.foundation.combinedClickable",
    "verticalScroll": "androidx.compose.foundation.verticalScroll",
    "rememberScrollState": "androidx.compose.foundation.rememberScrollState",
    "asImageBitmap": "androidx.compose.ui.graphics.asImageBitmap",
    "dp": "androidx.compose.ui.unit.dp",
    "sp": "androidx.compose.ui.unit.sp",
}

# 项目自有类型：用了简化名就必须 import（云端编译才报，坑过多次）
PROJECT_SYMBOLS = {
    "LxApp": "com.lingxu.music.LxApp",
    "SourceConst": "com.lingxu.music.core.model.SourceConst",
    "MusicInfo": "com.lingxu.music.core.model.MusicInfo",
    "GlassSurface": "com.lingxu.music.ui.components.GlassSurface",
    "GlassTopBar": "com.lingxu.music.ui.components.GlassTopBar",
    "GlassDialog": "com.lingxu.music.ui.components.GlassDialog",
    "GlassFab": "com.lingxu.music.ui.components.GlassFab",
    "GlassCard": "com.lingxu.music.ui.components.GlassCard",
    "GlassHost": "com.lingxu.music.ui.components.GlassHost",
    # Miuix 基础组件（漏 import 同样只在云端编译才报）
    "Button": "top.yukonga.miuix.kmp.basic.Button",
    "TextField": "top.yukonga.miuix.kmp.basic.TextField",
    "Switch": "top.yukonga.miuix.kmp.basic.Switch",
    "Slider": "top.yukonga.miuix.kmp.basic.Slider",
    "RadioButton": "top.yukonga.miuix.kmp.basic.RadioButton",
}


def model_fields() -> dict[str, set[str]]:
    """所有 data class 的字段表（在 core/model 与 core/db 下收集）。"""
    fields: dict[str, set[str]] = {}
    for d in (BASE / "com/lingxu/music/core/model", BASE / "com/lingxu/music/core/db"):
        if not d.exists():
            continue
        for p in d.glob("*.kt"):
            s = p.read_text(encoding="utf-8")
            for m in re.finditer(r"data class (\w+)\s*\((.*?)\n\)", s, re.S):
                fields[m.group(1)] = set(re.findall(r"\bval\s+(\w+)\s*:", m.group(2)))
    return fields



def project_symbols() -> set[str]:
    """所有 com.lingxu.music.* 下的顶层符号全限定名（含嵌套类成员）。"""
    syms: set[str] = set()
    for p in BASE.rglob("*.kt"):
        s = p.read_text(encoding="utf-8")
        m = re.search(r"^package ([\w.]+)", s, re.M)
        if not m:
            continue
        pkg = m.group(1)
        # 顶层声明
        for d in re.finditer(
            r"^(?:@\w+\s*)*(?:internal |private )?"
            r"(?:data class|enum class|sealed class|sealed interface|value class|fun|class|object|interface|val|var)\s+(\w+)",
            s,
            re.M,
        ):
            syms.add(f"{pkg}.{d.group(1)}")
        # 嵌套成员：ClassName.Member
        for cls in re.finditer(r"^(?:data class|class|object|enum class|sealed class)\s+(\w+)", s, re.M):
            cname = cls.group(1)
            for mem in re.finditer(r"\n    (?:fun|val|var|class|object|sealed class|data class|enum class)\s+(\w+)", s[cls.end():]):
                syms.add(f"{pkg}.{cname}.{mem.group(1)}")
    return syms


def check_dangling_imports() -> list[str]:
    """项目内 import 但符号不存在 —— 典型场景：组件被误删/改名而调用方没跟上。"""
    syms = project_symbols()
    issues: list[str] = []
    for p in sorted(BASE.rglob("*.kt")):
        s = p.read_text(encoding="utf-8")
        for m in re.finditer(r"^import (com\.lingxu\.music\.[\w.]+)$", s, re.M):
            fq = m.group(1)
            # 扩展函数等动态成员不做强校验：只查明显的形式
            if fq not in syms and not fq.split(".")[-1][0].islower():
                issues.append(f"{p.relative_to(ROOT)} import {fq} 找不到定义（符号被删或改名？）")
    return issues


def check_file(p: pathlib.Path, fields: dict[str, set[str]]) -> list[str]:
    issues: list[str] = []
    s = p.read_text(encoding="utf-8")
    rel = p.relative_to(ROOT)

    # 1) import 粘连 / 非法 import 行
    for i, line in enumerate(s.split("\n"), 1):
        if line.startswith("import ") and (
            line.count("import ") > 1 or not re.fullmatch(r"import [\w.]+", line.strip())
        ):
            issues.append(f"{rel}:{i} import 行非法: {line.strip()[:70]}")

    # 2) MiuixIcons.X 必须有对应 import
    imported_icons = set(re.findall(r"^import top\.yukonga\.miuix\.kmp\.icon\.extended\.(\w+)$", s, re.M))
    for m in re.finditer(r"MiuixIcons\.([A-Z]\w*)", s):
        icon = m.group(1)
        if icon not in imported_icons:
            issues.append(f"{rel} 使用了 MiuixIcons.{icon} 但未 import")

    # 3) layout 符号必须 import
    for sym in LAYOUT_SYMBOLS:
        used = re.search(r"(?<![\w.])" + sym + r"\s*\(", s)
        imported = re.search(r"^import androidx\.compose\.foundation\.layout\." + sym + r"$", s, re.M)
        if used and not imported:
            issues.append(f"{rel} 使用了 {sym}() 但未 import androidx.compose.foundation.layout.{sym}")

    # 3b) 项目自有类型用了简化名必须 import（含 `Sym.` 与 `Sym(` 两种用法）
    for sym, fqcn in PROJECT_SYMBOLS.items():
        used = re.search(r"(?<![\w.])" + sym + r"\s*[.(]", s)
        if not used:
            continue
        # 任意包下同名符号的 import 都算已导入（如 RadioButton 可能来自 material3）
        if re.search(r"^import [\w.]*\." + sym + r"$", s, re.M):
            continue
        # 同文件自己定义的符号无需 import
        if re.search(r"^(?:@\w+\s*)*(?:fun|class|object|interface|val)\s+" + sym + r"\b", s, re.M):
            continue
        # 同包内的符号无需 import
        pkg_m = re.search(r"^package ([\w.]+)", s, re.M)
        if pkg_m and fqcn == f"{pkg_m.group(1)}.{sym}":
            continue
        issues.append(f"{rel} 使用了 {sym} 但未 import {fqcn}")

    # 3c) Compose 扩展函数必须 import
    for name, fqcn in COMPOSE_EXTENSIONS.items():
        # .collectAsState( / .dp / .sp 这类用法
        if re.search(r"\." + name + r"\s*[( \n]", s) or re.search(r"\d+\." + name + r"\b", s) or re.search(r"\." + name + r"\b", s):
            if f"import {fqcn}\n" not in s:
                issues.append(f"{rel} 使用了 .{name} 但未 import {fqcn}")

    # 5) 括号平衡
    if s.count("{") != s.count("}"):
        issues.append(f"{rel} 花括号不平衡: {{={s.count('{')} }}={s.count('}')}")
    if s.count("(") != s.count(")"):
        issues.append(f"{rel} 圆括号不平衡: (={s.count('(')} )={s.count(')')}")

    # 5) 包名/库名笔误
    if "com.lingxu.mobile" in s or "muii" in s:
        issues.append(f"{rel} 包名或库名笔误（com.lingxu.mobile / muii）")

    # 6) 赋值写成函数调用
    for i, line in enumerate(s.split("\n"), 1):
        if re.search(r"=\s*(?:false|true)\(\)", line):
            issues.append(f"{rel}:{i} 赋值被写成函数调用: {line.strip()[:70]}")

    # 6b) 重复注解
    if re.search(r"@Composable\s*\n\s*@Composable", s):
        issues.append(f"{rel} 出现重复的 @Composable 注解")

    # 7) clickable 冲突
    if "combinedClickable" in s:
        for m in re.finditer(r"\.combinedClickable\([^)]*\)", s):
            pass
    # Card(onClick 与 modifier.combinedClickable 同时出现即告警
    if re.search(r"Card\((?:(?!\n\s*\))??)*?onClick\s*=", s) and "combinedClickable" in s:
        for m in re.finditer(r"Card\(", s):
            window = s[m.start() : m.start() + 600]
            if "combinedClickable" in window and re.search(r"^\s*onClick\s*=", window, re.M):
                issues.append(f"{rel} Card 同时使用 onClick 参数与 combinedClickable，点击行为会冲突")
                break

    return issues


def check_fields(s: str, rel: pathlib.Path, fields: dict[str, set[str]]) -> list[str]:
    """检查 <var>.meta.<field> / <var>.qualitys 之类对本项目模型的字段访问是否真实存在。"""
    issues: list[str] = []
    # music.meta.X / info.meta.X
    for m in re.finditer(r"\b(?:music|info|item|current|song|m)\.meta\.(\w+)", s):
        field = m.group(1)
        if field in ("copy", "toString", "equals", "hashCode"):
            continue
        meta = fields.get("MusicMeta")
        if meta is not None and field not in meta:
            issues.append(f"{rel} 访问 meta.{field} 但 MusicMeta 没有该字段（音质在 MusicInfo 顶层：qualitys/_qualitys）")
    # music.qualitys（顶层）——本项目确实在顶层
    for m in re.finditer(r"\b(?:music|info|item|current|song|m)\.qualitys\b", s):
        mi = fields.get("MusicInfo")
        if mi is not None and "qualitys" not in mi:
            issues.append(f"{rel} 访问 qualitys 但 MusicInfo 没有该字段")
    return issues


def main() -> int:
    if not BASE.exists():
        print(f"未找到源码目录: {BASE}")
        return 2
    fields = model_fields()
    all_issues: list[str] = []
    all_issues.extend(check_dangling_imports())
    for p in sorted(BASE.rglob("*.kt")):
        file_issues = check_file(p, fields)
        file_issues.extend(check_fields(p.read_text(encoding="utf-8"), p.relative_to(ROOT), fields))
        all_issues.extend(file_issues)
    for i in all_issues:
        print(i)
    print(f"\n共 {len(all_issues)} 处问题" if all_issues else "\n静态自检通过")
    return 1 if all_issues else 0


if __name__ == "__main__":
    sys.exit(main())
