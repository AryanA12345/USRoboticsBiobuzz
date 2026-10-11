{
  "startPoint": {
    "x": 133.55857740585776,
    "y": 8.296025104602508,
    "locked": false,
    "headingDeg": 90
  },
  "lines": [
    {
      "id": "line-mv41bbji-hhgozw",
      "color": "#ffc516",
      "name": "Path 1",
      "locked": false,
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 59.25627615062761,
        "y": 8.296025104602508
      },
      "controlPoints": [
        {
          "x": 89.64958158995816,
          "y": 63.55962343096235
        }
      ],
      "heading": {
        "type": "linear",
        "startDeg": 90,
        "endDeg": 90
      }
    },
    {
      "id": "line-mv41vjzz-dklelt",
      "color": "#C9CA89",
      "name": "",
      "locked": false,
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 59.81589958158997,
        "y": 38.11610878661088
      },
      "controlPoints": [],
      "heading": {
        "type": "tangential",
        "reverse": false
      }
    },
    {
      "id": "line-mv41vpjo-qk56ww",
      "color": "#B96566",
      "name": "",
      "locked": false,
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 9.769874476987447,
        "y": 97.63179916317993
      },
      "controlPoints": [
        {
          "x": 19.419456066945596,
          "y": 25.588912133891206
        }
      ],
      "heading": {
        "type": "linear",
        "reverse": false,
        "piecewiseHeading": {
          "segments": [
            {
              "startProgress": 0,
              "endProgress": 0.14256292558365757,
              "interpolationType": "linear",
              "reversed": false,
              "continueFromPrevious": false,
              "parameters": {
                "startDeg": 0,
                "endDeg": 0
              }
            },
            {
              "startProgress": 0.14256292558365757,
              "endProgress": 1,
              "interpolationType": "linear",
              "reversed": false,
              "continueFromPrevious": false,
              "parameters": {
                "startDeg": 0,
                "endDeg": 0
              }
            }
          ]
        },
        "degrees": 0,
        "startDeg": 90,
        "endDeg": 0
      }
    }
  ],
  "shapes": [
    {
      "id": "triangle-1",
      "name": "Red Goal",
      "vertices": [
        {
          "x": 141.5,
          "y": 70
        },
        {
          "x": 141.5,
          "y": 141.5
        },
        {
          "x": 118.3,
          "y": 141.5
        },
        {
          "x": 135.5,
          "y": 118
        },
        {
          "x": 136.3,
          "y": 70.2
        }
      ],
      "color": "#dc2626",
      "fillColor": "#ff6b6b"
    },
    {
      "id": "triangle-2",
      "name": "Blue Goal",
      "vertices": [
        {
          "x": 6.2,
          "y": 116.9
        },
        {
          "x": 25,
          "y": 141.5
        },
        {
          "x": 0,
          "y": 141.5
        },
        {
          "x": 0,
          "y": 70
        },
        {
          "x": 6,
          "y": 70
        }
      ],
      "color": "#2563eb",
      "fillColor": "#60a5fa"
    }
  ],
  "sequence": [
    {
      "kind": "path",
      "lineId": "line-mv41bbji-hhgozw"
    },
    {
      "kind": "path",
      "lineId": "line-mv41vjzz-dklelt"
    },
    {
      "kind": "path",
      "lineId": "line-mv41vpjo-qk56ww"
    }
  ],
  "fieldPoints": [],
  "version": "1.5.0",
  "timestamp": "2026-10-11T16:44:39.060Z"
}