import { BottomNav } from "@/components/layout/BottomNav";

export function AppShell({ children }: { children: React.ReactNode }) {
  return (
    <div className="mx-auto flex min-h-dvh w-full max-w-lg flex-col bg-[#0f0f12] text-zinc-100">
      <main className="flex-1 pb-20">{children}</main>
      <BottomNav />
    </div>
  );
}
