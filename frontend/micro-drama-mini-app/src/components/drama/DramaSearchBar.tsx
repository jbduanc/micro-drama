"use client";

import { Search } from "lucide-react";

type Props = {
  value: string;
  onChange: (value: string) => void;
  placeholder?: string;
};

export function DramaSearchBar({ value, onChange, placeholder }: Props) {
  return (
    <div className="relative">
      <Search className="pointer-events-none absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-zinc-500" />
      <input
        type="search"
        value={value}
        onChange={(e) => onChange(e.target.value)}
        placeholder={placeholder ?? "搜索短剧"}
        className="h-11 w-full rounded-xl border border-white/10 bg-white/5 pl-10 pr-4 text-sm outline-none ring-amber-400/40 placeholder:text-zinc-500 focus:border-amber-400/50 focus:ring-2"
      />
    </div>
  );
}
