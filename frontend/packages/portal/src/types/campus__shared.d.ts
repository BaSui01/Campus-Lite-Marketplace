/**
 * 临时类型声明文件
 * 等 shared 包的 DTS 生成修复后可以删除
 */
declare module '@campus/shared' {
  export * from '@campus/shared/index';
}

declare module '@campus/shared/api' {
  export const DefaultApi: any;
}

declare module '@campus/shared/api/models' {
  export type Event = any;
  export type MarketingCampaign = any;
  export type Resource = any;
  export type Goods = any;
  export type Order = any;
  export type User = any;
  export type Review = any;
  export type Dispute = any;
  export type Notification = any;
  export type LogisticsTrack = any;
}

declare module '@campus/shared/components' {
  export const Avatar: any;
  export const Button: any;
  export const Loading: any;
  export const Modal: any;
  export const Pagination: any;
  export const FilterPanel: any;
  export const GoodsCard: any;
  export const OrderCard: any;

}

declare module '@campus/shared/utils' {
  export const getApi: () => any;
  export const formatDate: (date: any) => string;
  export const showToast: (message: string, type?: string) => void;
  export const showSuccess: (message: string) => void;
  export const showError: (message: string) => void;

}

declare module '@campus/shared/utils/apiClient' {
  export const apiClient: any;
  export const getApi: () => any;
}

declare module '@campus/shared/services' {
  export const goodsService: any;
  export const orderService: any;
  export const userService: any;
  export const authService: any;
  export const reviewService: any;
  export const disputeService: any;
  export const notificationService: any;
  export const logisticsService: any;
  export const favoriteService: any;
  export const tagService: any;
  export const categoryService: any;
  export const campusService: any;
  export const topicService: any;
  export const postService: any;
  export const refundService: any;
  export const marketingService: any;

}

declare module '@campus/shared/services/auth' {
  export const sendVerificationCode: (phone: string) => Promise<any>;
  export const verifyCode: (phone: string, code: string) => Promise<any>;

}

declare module '@campus/shared/services/captcha' {
  export const getCaptcha: () => Promise<any>;
  export const verifyCaptcha: (data: any) => Promise<any>;

}

declare module '@campus/shared/services/goods' {
  export const getGoodsDetail: (id: number) => Promise<any>;
  export const listGoods: (params: any) => Promise<any>;

}

declare module '@campus/shared/services/order' {
  export const createOrder: (data: any) => Promise<any>;
  export const getOrderDetail: (id: number) => Promise<any>;
  export const listOrders: (params: any) => Promise<any>;

}

declare module '@campus/shared/services/user' {
  export const getUserInfo: (id: number) => Promise<any>;
  export const updateUserInfo: (data: any) => Promise<any>;

}

declare module '@campus/shared/services/message' {
  export const sendMessage: (data: any) => Promise<any>;
  export const listMessages: (params: any) => Promise<any>;

}

declare module '@campus/shared/services/marketing' {
  export const listCampaigns: (params: any) => Promise<any>;
  export const getCampaignDetail: (id: number) => Promise<any>;

}

declare module '@campus/shared/services/tag' {
  export const listTags: (params: any) => Promise<any>;

}

declare module '@campus/shared/services/topic' {
  export const listTopics: (params: any) => Promise<any>;

}

declare module '@campus/shared/types' {
  export type UserRole = 'admin' | 'user' | 'seller';
  export type OrderStatus = 'pending' | 'paid' | 'shipped' | 'completed' | 'cancelled';
  export type GoodsStatus = 'draft' | 'published' | 'sold' | 'removed';

}

declare module '@campus/shared/types/emoji' {
  export interface Emoji {
    id: number;
    code: string;
    name: string;
    category: string;
  }
  export type EmojiCategory = 'smile' | 'gesture' | 'people' | 'nature' | 'food' | 'travel' | 'activity' | 'object' | 'symbol' | 'flag';

}

declare module '@campus/shared/types/search' {
  export interface SearchFilters {
    keyword?: string;
    category?: string;
    priceMin?: number;
    priceMax?: number;
  }
  export interface SearchResult {
    id: number;
    title: string;
    description: string;
  }
  export interface QuickSearch {
    id: number;
    keyword: string;
  }
  export type MessageType = 'text' | 'image' | 'system';
  export const SEARCH_CONFIG: any;
  export const PRESET_QUICK_SEARCHES: any;

}

declare module '@campus/shared/types/captcha' {
  export type CaptchaType = 'slider' | 'rotate' | 'click';
  export interface CaptchaData {
    type: CaptchaType;
    image: string;
    token: string;
  }

}

declare module '@campus/shared/hooks' {
  export const useAuth: () => any;
  export const useUser: () => any;
  export const useUpload: (options?: any) => any;
  export const useDebounce: (value: any, delay: number) => any;
  export const useThrottle: (callback: any, delay: number) => any;
  export const useWebSocket: (url: string, options?: any) => any;

}

declare module '@campus/shared/utils/highlight' {
  export const highlightText: (text: string, keyword: string) => string;

}
