-- 添加收藏标记字段到查询历史表
ALTER TABLE query_history
ADD COLUMN IF NOT EXISTS favorite BOOLEAN DEFAULT FALSE; 