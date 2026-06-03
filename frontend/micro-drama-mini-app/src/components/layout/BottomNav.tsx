"use client";

import Link from "next/link";
import { usePathname } from "next/navigation";
import { Clapperboard, PlayCircle, UserRound } from "lucide-react";
import { cn } from "@/lib/utils";

const tabs = [
  { href: "/dramas", label: "剧集", icon: Clapperboard },
  { href: "/play", label: "播放", icon: PlayCircle },
  { href: "/profile", label: "我的", icon: UserRound },
];

export function BottomNav() {
  const pathname = usePathname();

  return (
    <nav className="fixed inset-x-0 bottom-0 z-50 border-t border-white/10 bg-[#0f0f12]/95 backdrop-blur-md">
      <div className="mx-auto flex h-16 max-w-lg items-stretch px-2 pb-[env(safe-area-inset-bottom)]">
        {tabs.map(({ href, label, icon: Icon }) => {
          const active =
            pathname === href ||
            (href === "/dramas" && pathname.startsWith("/dramas")) ||
            (href === "/profile" && pathname.startsWith("/profile"));
          return (
            <Link
              key={href}
              href={href}
              className={cn(
                "flex flex-1 flex-col items-center justify-center gap-1 text-xs transition-colors",
                active ? "text-amber-400" : "text-zinc-400 hover:text-zinc-200",
              )}
            >
              <Icon className={cn("h-5 w-5", active && "fill-amber-400/20")} />
              <span>{label}</span>
            </Link>
          );
        })}
      </div>
    </nav>
  );
}
