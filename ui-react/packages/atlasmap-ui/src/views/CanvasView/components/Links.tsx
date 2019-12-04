import React, { FunctionComponent } from 'react';
import { CanvasLink } from '../../../canvas';
import { IMappings } from '../models';
import { useCanvasViewContext } from '../CanvasViewProvider';
import { useMappingLinks } from './useMappingLinks';
import { useSourceTargetLinks } from './useSourceTargetLinks';

export interface ILinksProps {
  mappings: IMappings[];
  selectedMapping: string | undefined;
}
export const Links: FunctionComponent<ILinksProps> = ({ mappings, selectedMapping }) => {
  const { materializedMappings } = useCanvasViewContext();
  const { links: smtLinks } = useMappingLinks({ mappings, selectedMapping });
  const { links: stLinks } = useSourceTargetLinks({ mappings });
  return (
    <g>
      {(materializedMappings ? smtLinks : stLinks).map(({ id, start, end, color }) => (
        <CanvasLink key={id} start={start} end={end} color={color} />
      ))}
    </g>
  );
};
