/**
 * AppealFileUploader 组件测试
 * @author BaSui 😎
 * @description 测试申诉材料上传组件的各项功能
 */

import React from 'react';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import '@testing-library/jest-dom';
import { AppealFileUploader } from './AppealFileUploader';

// Mock the upload hook
jest.mock('@campus/shared/hooks', () => ({
  useUpload: jest.fn(),
}));

import { useUpload } from '@campus/shared/hooks';

// Mock appeal service
jest.mock('../../../services', () => ({
  appealService: {
    addAppealMaterial: jest.fn(),
  },
}));

describe('AppealFileUploader', () => {
  const mockUpload = jest.fn();

  beforeEach(() => {
    (useUpload as jest.Mock).mockReturnValue({
      fileList: [],
      uploading: false,
      upload: mockUpload,
      remove: jest.fn(),
    });

    mockUpload.mockClear();
  });

  it('renders upload area', () => {
    render(<AppealFileUploader />);

    expect(screen.getByText(/拖拽文件到此处或点击上传/)).toBeInTheDocument();
    expect(screen.getByText(/支持：JPG、PNG、GIF、WebP、PDF、Word文档/)).toBeInTheDocument();
    expect(screen.getByText(/图片 ≤ 10MB，文档 ≤ 20MB，最多 5 个文件/)).toBeInTheDocument();
  });

  it('shows upload button when showUploadButton is true', () => {
    render(<AppealFileUploader showUploadButton />);

    expect(screen.getByRole('button', { name: /选择文件/ })).toBeInTheDocument();
  });

  it('does not show upload button when showUploadButton is false', () => {
    render(<AppealFileUploader showUploadButton={false} />);

    expect(screen.queryByRole('button', { name: /选择文件/ })).not.toBeInTheDocument();
  });

  it('disables uploader when disabled prop is true', () => {
    render(<AppealFileUploader disabled />);

    const uploadArea = screen.getByText(/拖拽文件到此处或点击上传/).parentElement;
    expect(uploadArea).toHaveClass('opacity-50', 'cursor-not-allowed');
  });

  it('calls upload when files are selected', async () => {
    const mockFiles = [
      new File(['test'], 'test.jpg', { type: 'image/jpeg' }),
    ];

    mockUpload.mockResolvedValue(undefined);

    render(<AppealFileUploader />);

    const fileInput = screen.getByRole('button', { name: /选择文件/ }).nextElementSibling as HTMLInputElement;

    fireEvent.change(fileInput, { target: { files: mockFiles } });

    await waitFor(() => {
      expect(mockUpload).toHaveBeenCalledWith(mockFiles);
    });
  });

  it('handles drag and drop', async () => {
    const mockFiles = [
      new File(['test'], 'test.jpg', { type: 'image/jpeg' }),
    ];

    mockUpload.mockResolvedValue(undefined);

    render(<AppealFileUploader />);

    const uploadArea = screen.getByText(/拖拽文件到此处或点击上传/).parentElement;

    fireEvent.dragEnter(uploadArea!);
    fireEvent.dragOver(uploadArea!);
    fireEvent.drop(uploadArea!, {
      dataTransfer: { files: mockFiles },
    });

    await waitFor(() => {
      expect(mockUpload).toHaveBeenCalledWith(mockFiles);
    });
  });

  it('shows drag over state', () => {
    render(<AppealFileUploader />);

    const uploadArea = screen.getByText(/拖拽文件到此处或点击上传/).parentElement;

    fireEvent.dragEnter(uploadArea!);

    expect(uploadArea).toHaveClass('border-blue-500', 'bg-blue-50');
  });

  it('validates file count limit', () => {
    (useUpload as jest.Mock).mockReturnValue({
      fileList: Array(5).fill({ uid: '1', status: 'success' }),
      uploading: false,
      upload: mockUpload,
      remove: jest.fn(),
    });

    const mockFiles = [
      new File(['test'], 'test.jpg', { type: 'image/jpeg' }),
    ];

    render(<AppealFileUploader />);

    const fileInput = screen.getByRole('button', { name: /选择文件/ }).nextElementSibling as HTMLInputElement;

    fireEvent.change(fileInput, { target: { files: mockFiles } });

    // Should not call upload due to file count limit
    expect(mockUpload).not.toHaveBeenCalled();
  });

  it('displays uploaded files', () => {
    const mockFiles = [
      {
        uid: '1',
        name: 'test.jpg',
        size: 1024,
        type: 'image/jpeg',
        file: new File(['test'], 'test.jpg'),
        status: 'success' as const,
        progress: 100,
        url: 'http://example.com/test.jpg',
      },
    ];

    (useUpload as jest.Mock).mockReturnValue({
      fileList: mockFiles,
      uploading: false,
      upload: mockUpload,
      remove: jest.fn(),
    });

    render(<AppealFileUploader />);

    expect(screen.getByText('test.jpg')).toBeInTheDocument();
    expect(screen.getByText('JPG图片')).toBeInTheDocument();
    expect(screen.getByText('1 KB')).toBeInTheDocument();
    expect(screen.getByRole('button', { name: /查看/ })).toBeInTheDocument();
    expect(screen.getByRole('button', { name: /删除/ })).toBeInTheDocument();
  });

  it('shows upload progress', () => {
    const mockFiles = [
      {
        uid: '1',
        name: 'test.jpg',
        size: 1024,
        type: 'image/jpeg',
        file: new File(['test'], 'test.jpg'),
        status: 'uploading' as const,
        progress: 50,
      },
    ];

    (useUpload as jest.Mock).mockReturnValue({
      fileList: mockFiles,
      uploading: true,
      upload: mockUpload,
      remove: jest.fn(),
    });

    render(<AppealFileUploader />);

    expect(screen.getByText('正在上传...')).toBeInTheDocument();
    expect(screen.getByText('test.jpg')).toBeInTheDocument();
    expect(screen.getByText('50%')).toBeInTheDocument();
  });

  it('shows error messages', () => {
    const mockFiles = [
      {
        uid: '1',
        name: 'test.jpg',
        size: 1024,
        type: 'image/jpeg',
        file: new File(['test'], 'test.jpg'),
        status: 'error' as const,
        progress: 0,
        error: 'Upload failed',
      },
    ];

    (useUpload as jest.Mock).mockReturnValue({
      fileList: mockFiles,
      uploading: false,
      upload: mockUpload,
      remove: jest.fn(),
    });

    render(<AppealFileUploader />);

    expect(screen.getByText('Upload failed')).toBeInTheDocument();
  });
});