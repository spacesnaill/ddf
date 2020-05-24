import { LazyQueryResult } from './LazyQueryResult'
import * as React from 'react'
const _ = require('underscore')

export const useSelectionOfLazyResult = ({
  lazyResult,
}: {
  lazyResult: LazyQueryResult
}) => {
  const [isSelected, setIsSelected] = React.useState(lazyResult.isSelected)
  React.useEffect(() => {
    const id = lazyResult.subscribeToSelection(() => {
      setIsSelected(lazyResult.isSelected)
    })
    return () => {
      lazyResult.unsubscribeFromSelection(id)
    }
  }, [])
  return isSelected
}

export const useFilteredOfLazyResult = ({
  lazyResult,
}: {
  lazyResult: LazyQueryResult
}) => {
  const [isFiltered, setIsFiltered] = React.useState(lazyResult.isFiltered)
  React.useEffect(() => {
    const unsubscribe = lazyResult.subscribeToFiltered(() => {
      setIsFiltered(lazyResult.isFiltered)
    })
    return () => {
      unsubscribe()
    }
  }, [])
  return isFiltered
}

type useSelectionOfLazyResultsReturn = 'unselected' | 'partially' | 'selected'

export const useSelectionOfLazyResults = ({
  lazyResults,
}: {
  lazyResults: LazyQueryResult[]
}) => {
  const cache = React.useRef(
    lazyResults.reduce(
      (blob, lazyResult) => {
        blob[lazyResult['metacard.id']] = lazyResult.isSelected
        return blob
      },
      {} as { [key: string]: boolean }
    )
  )
  const calculateIfSelected = React.useMemo(() => {
    return () => {
      const currentValues = Object.values(cache.current)
      let baseline = currentValues[0]
      let updateToIsSelected = baseline
        ? 'selected'
        : ('unselected' as useSelectionOfLazyResultsReturn)
      for (let i = 1; i <= currentValues.length - 1; i++) {
        if (baseline !== currentValues[i]) {
          updateToIsSelected = 'partially'
          break
        }
      }
      return updateToIsSelected
    }
  }, [])
  const debouncedUpdatedIsSelected = React.useMemo(() => {
    return _.debounce(() => {
      setIsSelected(calculateIfSelected())
    }, 100)
  }, [])

  const [isSelected, setIsSelected] = React.useState(
    calculateIfSelected() as useSelectionOfLazyResultsReturn
  )

  React.useEffect(() => {
    const unsubscribeCalls = lazyResults.map(lazyResult => {
      const id = lazyResult.subscribeToSelection(() => {
        cache.current[lazyResult['metacard.id']] = lazyResult.isSelected
        debouncedUpdatedIsSelected()
      })
      return () => {
        lazyResult.unsubscribeFromSelection(id)
      }
    })
    return () => {
      unsubscribeCalls.forEach(unsubscribeCall => {
        unsubscribeCall()
      })
    }
  }, [])
  return isSelected
}

export const useBackboneOfLazyResult = ({
  lazyResult,
}: {
  lazyResult: LazyQueryResult
}) => {
  const [backboneModel, setBackboneModel] = React.useState(
    lazyResult.backbone as Backbone.Model | undefined
  )
  React.useEffect(() => {
    let id = undefined as undefined | string
    if (!lazyResult.backbone) {
      id = lazyResult.subscribe(() => {
        setBackboneModel(lazyResult.backbone)
      })
    }
    return () => {
      lazyResult.unsubscribe(id)
    }
  }, [])
  return backboneModel
}
