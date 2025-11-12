/**
 * Tasks - 任务管理列表
 * @author BaSui 😎
 */

import React from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { taskService } from '@campus/shared/services/task';

const Tasks: React.FC = () => {
  const queryClient = useQueryClient();

  const { data: tasks, isLoading } = useQuery({
    queryKey: ['tasks'],
    queryFn: () => taskService.list(),
  });

  const triggerMutation = useMutation({
    mutationFn: (name: string) => taskService.trigger(name),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['tasks'] });
    },
  });

  const pauseMutation = useMutation({
    mutationFn: (name: string) => taskService.pause(name),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['tasks'] });
    },
  });

  const resumeMutation = useMutation({
    mutationFn: (name: string) => taskService.resume(name),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['tasks'] });
    },
  });

  if (isLoading) return <div>加载中...</div>;

  return (
    <div className="tasks">
      <h1>任务管理</h1>
      <div className="tasks__list">
        {tasks?.map((task) => (
          <div key={task.name} className="task-card">
            <div className="task-card__header">
              <h3>{task.description}</h3>
              <span className={`task-status task-status--${task.status}`}>
                {task.status}
              </span>
            </div>
            <div className="task-card__actions">
              <button
                onClick={() => triggerMutation.mutate(task.name)}
                disabled={task.status === 'PAUSED'}
              >
                触发执行
              </button>
              {task.status === 'ENABLED' ? (
                <button onClick={() => pauseMutation.mutate(task.name)}>
                  暂停
                </button>
              ) : (
                <button onClick={() => resumeMutation.mutate(task.name)}>
                  恢复
                </button>
              )}
            </div>
          </div>
        ))}
      </div>
    </div>
  );
};

export default Tasks;
