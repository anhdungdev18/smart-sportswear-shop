export type SiteSettingResponse = {
  id: string;
  settingKey: string;
  settingValue: string | null;
  valueType: string;
  description: string | null;
  isPublic: boolean;
  updatedBy: string | null;
  createdAt: string;
  updatedAt: string;
};
