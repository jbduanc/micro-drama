import { ProfileHeader } from "@/components/profile/ProfileHeader";

export default function ProfilePage() {
  return (
    <div className="px-4 pt-4">
      <h1 className="mb-4 text-2xl font-bold">个人中心</h1>
      <ProfileHeader />
    </div>
  );
}
