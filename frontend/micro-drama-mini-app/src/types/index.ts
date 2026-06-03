export type MicroDramaStatus = 0 | 1;

export type DramaEpisode = {
  id?: string;
  episodeNum: number;
  title: string;
  videoAssetId?: string;
  duration?: number;
  price?: number;
};

export type MicroDrama = {
  id?: string;
  title?: string;
  coverUrl?: string;
  description?: string;
  totalEpisodes?: number;
  price?: number;
  status?: MicroDramaStatus;
  sort?: number;
  episodes?: DramaEpisode[];
};

export type TablePageInfo<T> = {
  data?: T[];
  list?: T[];
  total: number;
};

export type ApiResult<T> = {
  code?: number;
  msg?: string;
  message?: string;
  data: T;
};

export type PlayAuthResponse = {
  playUrl?: string;
  expiresAt?: string;
};

export type PaymentOrder = {
  id: string;
  status: string;
  amount?: number;
  dramaTitle?: string;
  episodeTitle?: string;
  method?: "web2" | "web3";
  createdAt?: string;
};

export type WatchRecord = {
  id: string;
  dramaId: string;
  dramaTitle: string;
  episodeId: string;
  episodeTitle: string;
  episodeNum: number;
  coverUrl?: string;
  watchedAt: string;
  progressSeconds?: number;
};

export type UserProfile = {
  id: string;
  nickname: string;
  avatarUrl?: string;
  balance: number;
  telegramId?: string;
};
