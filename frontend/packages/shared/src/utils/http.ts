/**
 * 基于共享 axiosInstance 的轻量 HTTP 封装
 */

import type { AxiosRequestConfig } from 'axios';
import { axiosInstance } from './apiClient';

const extractData = <T>(promise: Promise<{ data: T }>): Promise<T> => {
  return promise.then((response) => response.data);
};

export const http = {
  get<T = any>(url: string, config?: AxiosRequestConfig) {
    return extractData<T>(axiosInstance.get<T>(url, config));
  },

  post<T = any>(url: string, data?: any, config?: AxiosRequestConfig) {
    return extractData<T>(axiosInstance.post<T>(url, data, config));
  },

  put<T = any>(url: string, data?: any, config?: AxiosRequestConfig) {
    return extractData<T>(axiosInstance.put<T>(url, data, config));
  },

  patch<T = any>(url: string, data?: any, config?: AxiosRequestConfig) {
    return extractData<T>(axiosInstance.patch<T>(url, data, config));
  },

  delete<T = any>(url: string, config?: AxiosRequestConfig) {
    return extractData<T>(axiosInstance.delete<T>(url, config));
  },
};

export type HttpClient = typeof http;
